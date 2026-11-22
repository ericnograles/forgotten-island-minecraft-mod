#!/usr/bin/env python3
"""Compare a stopped/flushed 0.2 world with its 0.3 upgrade; never modify either."""
import argparse
import gzip
import json
from pathlib import Path

from verify_adventure_save import NBT, region_chunks, snapshot


def journal(world):
    return NBT(gzip.decompress((world / 'data/forgottenisland_adventure.dat').read_bytes())).root()['data']


def old_blocks(world, built):
    result = {}
    for path in (world / 'dimensions/forgottenisland/forgotten_island/region').glob('*.mca'):
        for _, chunk in region_chunks(path):
            x, z = chunk['xPos'], chunk['zPos']
            packed = ((z & 0xffffffff) << 32) | (x & 0xffffffff)
            if packed >= 1 << 63:
                packed -= 1 << 64
            # Protochunks and baseline ocean may legitimately finish construction on first load.
            if packed in built and -208 <= x * 16 and x * 16 + 15 < 176 and -160 <= z * 16 <= 144:
                result[f'{x},{z}'] = {
                    str(section['Y']): section['block_states']
                    for section in chunk.get('sections', []) if 'block_states' in section
                }
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('before', type=Path)
    parser.add_argument('after', type=Path)
    parser.add_argument('output', type=Path)
    args = parser.parse_args()
    before, after = snapshot(args.before), snapshot(args.after, expected_residents=None)
    for key in ('departures', 'portals', 'arrival_portal', 'encounters', 'rewards', 'exits'):
        assert before[key] == after[key], f'Changed durable {key}'
    assert all(after['residents'].get(key) == value for key, value in before['residents'].items()), 'Lost or changed original resident'
    old, new = journal(args.before), journal(args.after)
    assert set(old['BuiltChunks']) <= set(new['BuiltChunks']), 'Lost legacy built-chunk journal'
    assert new.get('ExpandedChunks'), 'Expansion was not initialized'
    built = set(old['BuiltChunks'])
    blocks_before, blocks_after = old_blocks(args.before, built), old_blocks(args.after, built)
    assert blocks_before, 'No original district chunks found'
    assert all(blocks_after.get(key) == value for key, value in blocks_before.items()), 'Changed original district blocks'
    report = {
        'result': 'PASS', 'original_residents_retained': len(before['residents']),
        'total_saved_residents': len(after['residents']),
        'original_district_chunks_unchanged': len(blocks_before),
        'legacy_built_markers_retained': len(old['BuiltChunks']),
        'expanded_chunks': len(new['ExpandedChunks']),
        'preserved': ['individual return links', 'manual portal registration and blocks',
                      'completed encounters', 'unique collected reward inventory/tokens', 'original resident identities'],
    }
    args.output.write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps(report, indent=2))


if __name__ == '__main__':
    main()

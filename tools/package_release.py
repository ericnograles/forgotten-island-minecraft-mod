#!/usr/bin/env python3
"""Package the release using explicit source roots; never include local QA worlds."""
from hashlib import sha256
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile, ZipInfo

ROOT = Path(__file__).resolve().parents[1]
VERSION = "0.3.0"
JAR = ROOT / "build" / "libs" / f"forgotten-island-{VERSION}.jar"
OUTPUT = ROOT / "build" / "distributions"
SOURCE_ROOTS = (
    ".gitattributes", ".gitignore", "LICENSE", "README.md", "build.gradle", "settings.gradle",
    "gradle.properties", "gradlew", "gradlew.bat", "gradle", "src",
    "assets-src", "tools", "docs",
)
EXCLUDED_PARTS = {"__pycache__", ".DS_Store"}


def files_under(names):
    files = []
    for name in names:
        path = ROOT / name
        if not path.exists():
            raise FileNotFoundError(path)
        files.extend(path.rglob("*") if path.is_dir() else [path])
    return sorted(p for p in files if p.is_file()
                  and not EXCLUDED_PARTS.intersection(p.parts)
                  and not p.is_symlink())


def write_zip(path, entries):
    with ZipFile(path, "w", compression=ZIP_DEFLATED, compresslevel=9) as archive:
        for name, data, executable in sorted(entries):
            info = ZipInfo(name, date_time=(2026, 1, 1, 0, 0, 0))
            info.compress_type = ZIP_DEFLATED
            info.create_system = 3
            info.external_attr = (0o100755 if executable else 0o100644) << 16
            archive.writestr(info, data, compresslevel=9)
    with ZipFile(path) as archive:
        bad = archive.testzip()
        if bad:
            raise ValueError(f"Corrupt archive member: {bad}")


def main():
    jar_bytes = JAR.read_bytes()
    jar_hash = sha256(jar_bytes).hexdigest()
    OUTPUT.mkdir(parents=True, exist_ok=True)
    (OUTPUT / JAR.name).write_bytes(jar_bytes)
    release = OUTPUT / f"forgotten-island-{VERSION}.zip"
    source = OUTPUT / f"forgotten-island-{VERSION}-source.zip"
    entries = [(str(p.relative_to(ROOT)), p.read_bytes(), False)
               for p in files_under(("README.md", "LICENSE", "docs"))]
    entries.extend(((JAR.name, jar_bytes, False),
                    ("SHA256SUMS.txt", f"{jar_hash}  {JAR.name}\n".encode(), False)))
    write_zip(release, entries)
    prefix = f"forgotten-island-{VERSION}-source/"
    write_zip(source, [(prefix + str(p.relative_to(ROOT)), p.read_bytes(),
                        p.name == "gradlew") for p in files_under(SOURCE_ROOTS)])
    checksums = [(JAR.name, jar_hash)] + [
        (p.name, sha256(p.read_bytes()).hexdigest()) for p in (release, source)]
    (OUTPUT / "SHA256SUMS.txt").write_text(
        "".join(f"{digest}  {name}\n" for name, digest in checksums))
    for path in (release, source):
        print(f"{path.relative_to(ROOT)} ({path.stat().st_size:,} bytes)")
    print(f"JAR SHA-256: {jar_hash}")


if __name__ == "__main__":
    main()

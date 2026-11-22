"""Verify an existing GitHub release and record its CurseForge publication.

Uses only Python 3.11+ and gh. Never builds or modifies the release JAR.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tomllib
import zipfile

RECEIPT = "curseforge-publication.json"


def gh(*args):
    return subprocess.check_output(["gh", *args], text=True)


def verify_jar(path, checksums, version):
    matches = re.findall(r"^([a-fA-F0-9]{64}) [ *]" + re.escape(path.name) + r"$",
                         checksums, re.MULTILINE)
    if len(matches) != 1:
        raise ValueError("SHA256SUMS.txt must contain exactly one entry for the release JAR")
    digest = hashlib.sha256(path.read_bytes()).hexdigest()
    if digest != matches[0].lower():
        raise ValueError("Release JAR checksum does not match SHA256SUMS.txt")
    with zipfile.ZipFile(path) as archive:
        metadata = tomllib.loads(archive.read("META-INF/mods.toml").decode())
    mods = metadata.get("mods", [])
    if len(mods) != 1 or mods[0].get("modId") != "forgottenisland" or mods[0].get("version") != version:
        raise ValueError("Packaged mod ID/version does not match the release tag")
    dependencies = {d["modId"]: d for d in metadata["dependencies"]["forgottenisland"]}
    expected = {"minecraft": "[1.20.1,1.20.2)", "forge": "[47.4.10,48)"}
    if metadata.get("modLoader") != "javafml" or set(dependencies) != set(expected):
        raise ValueError("Update publishing configuration for changed loader/dependencies")
    for name, version_range in expected.items():
        if dependencies[name].get("versionRange") != version_range or dependencies[name].get("side") != "BOTH":
            raise ValueError("Update publishing configuration for changed compatibility")
    return digest


def receipt_matches(receipt, expected):
    return all(receipt.get(key) == value for key, value in expected.items()) and isinstance(receipt.get("file_id"), int) and receipt["file_id"] > 0


def output(values):
    if os.environ.get("GITHUB_OUTPUT"):
        with open(os.environ["GITHUB_OUTPUT"], "a") as stream:
            for key, value in values.items():
                stream.write(f"{key}={value}\n")


def prepare():
    tag = os.environ["RELEASE_TAG"]
    if not re.fullmatch(r"v\d+\.\d+\.\d+(?:-[A-Za-z0-9.]+)?", tag):
        raise ValueError("Use a version tag such as v0.4.0")
    repo = os.environ["GITHUB_REPOSITORY"]
    release = json.loads(gh("api", f"repos/{repo}/releases/tags/{tag}"))
    if release["draft"] or not release.get("published_at"):
        raise ValueError("Publish the GitHub release before syncing it")
    version = tag[1:]
    filename = f"forgotten-island-{version}.jar"
    assets = [a["name"] for a in release["assets"]]
    for name in (filename, "SHA256SUMS.txt"):
        if assets.count(name) != 1:
            raise ValueError(f"Attach exactly one {name} before publishing the GitHub release")
    destination = Path("build/curseforge-publish")
    destination.mkdir(parents=True, exist_ok=True)
    gh("release", "download", tag, "--repo", repo, "--dir", str(destination), "--clobber",
       "--pattern", filename, "--pattern", "SHA256SUMS.txt")
    digest = verify_jar(destination / filename, (destination / "SHA256SUMS.txt").read_text(), version)
    expected = {"repository": repo, "tag": tag, "release_id": release["id"],
                "project_id": os.environ.get("CURSEFORGE_PROJECT_ID", ""),
                "filename": filename, "sha256": digest}
    already_published = False
    if RECEIPT in assets:
        gh("release", "download", tag, "--repo", repo, "--dir", str(destination), "--clobber", "--pattern", RECEIPT)
        receipt = json.loads((destination / RECEIPT).read_text())
        if not receipt_matches(receipt, expected):
            raise ValueError("Publication receipt conflicts with this project/release/JAR; inspect it before retrying")
        already_published = True
        print(f"Already submitted to CurseForge as file {receipt['file_id']}; no duplicate upload")
    (destination / "release.json").write_text(json.dumps(expected, indent=2) + "\n")
    (destination / "changelog.md").write_text(release.get("body") or f"Forgotten Island {version}\n")
    output({"jar": str(destination / filename), "version": version,
            "release_type": "beta" if release["prerelease"] else "release",
            "already_published": str(already_published).lower()})
    print(f"Verified {filename}: {digest}")


def record():
    destination = Path("build/curseforge-publish")
    receipt = json.loads((destination / "release.json").read_text())
    file_id = int(os.environ["CURSEFORGE_FILE_ID"])
    if file_id <= 0 or not receipt["project_id"].isdigit():
        raise ValueError("A numeric CurseForge file ID and project ID are required")
    receipt["file_id"] = file_id
    path = destination / RECEIPT
    path.write_text(json.dumps(receipt, indent=2) + "\n")
    # No clobber: never silently replace a receipt for a different publication.
    gh("release", "upload", receipt["tag"], str(path), "--repo", receipt["repository"])
    print(f"Recorded CurseForge file {file_id} on GitHub release {receipt['tag']}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=["prepare", "record"])
    args = parser.parse_args()
    {"prepare": prepare, "record": record}[args.command]()

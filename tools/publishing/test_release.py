import hashlib
from pathlib import Path
import tempfile
import unittest
import zipfile

from release import receipt_matches, verify_jar


class ReleaseVerificationTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.jar = Path(self.temp.name) / "forgotten-island-0.5.0.jar"
        self.metadata = Path("src/main/resources/META-INF/mods.toml").read_text()

    def package(self, metadata=None):
        with zipfile.ZipFile(self.jar, "w") as archive:
            archive.writestr("META-INF/mods.toml", metadata or self.metadata)
        digest = hashlib.sha256(self.jar.read_bytes()).hexdigest()
        return f"{digest}  {self.jar.name}\n"

    def test_matching_release(self):
        checksums = self.package()
        self.assertEqual(verify_jar(self.jar, checksums, "0.5.0"), checksums[:64])

    def test_tampered_jar(self):
        checksums = self.package()
        with self.jar.open("ab") as stream:
            stream.write(b"tampered")
        with self.assertRaisesRegex(ValueError, "checksum"):
            verify_jar(self.jar, checksums, "0.5.0")

    def test_missing_or_ambiguous_checksum(self):
        checksums = self.package()
        for invalid in ("", checksums + checksums, checksums.replace(self.jar.name, "sources.jar")):
            with self.subTest(invalid=invalid), self.assertRaisesRegex(ValueError, "exactly one"):
                verify_jar(self.jar, invalid, "0.5.0")

    def test_wrong_packaged_version(self):
        with self.assertRaisesRegex(ValueError, "version"):
            verify_jar(self.jar, self.package(), "0.4.0")

    def test_changed_compatibility_requires_workflow_update(self):
        for before, after in (("[1.20.1,1.20.2)", "[1.21,1.22)"),
                              ('side="BOTH"', 'side="CLIENT"'),
                              ('modLoader="javafml"', 'modLoader="other"')):
            with self.subTest(before=before), self.assertRaises(ValueError):
                verify_jar(self.jar, self.package(self.metadata.replace(before, after)), "0.5.0")

    def test_receipt_is_bound_to_exact_release_and_project(self):
        expected = {"repository": "owner/repo", "tag": "v0.5.0", "release_id": 1,
                    "project_id": "123", "filename": self.jar.name, "sha256": "abc"}
        receipt = {**expected, "file_id": 456}
        self.assertTrue(receipt_matches(receipt, expected))
        for key in expected:
            with self.subTest(key=key):
                self.assertFalse(receipt_matches({**receipt, key: "different"}, expected))
        for file_id in (None, 0, "456"):
            self.assertFalse(receipt_matches({**receipt, "file_id": file_id}, expected))


if __name__ == "__main__":
    unittest.main()

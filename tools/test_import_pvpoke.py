import json
import unittest
from import_pvpoke import parse_cpms, parse_pokemon, git_blob_sha1


class ImportTests(unittest.TestCase):
    def test_blob_sha(self):
        # Git's well-known empty blob identifier.
        self.assertEqual(git_blob_sha1(b""), "e69de29bb2d1d6434b8b29ae775ad8c2e48c5391")

    def test_cpms_reject_short_table(self):
        with self.assertRaises(ValueError):
            parse_cpms(b"var cpms = [0.094, 0.135];")

    def test_master_reject_short_list(self):
        with self.assertRaises(ValueError):
            parse_pokemon(json.dumps({"pokemon": []}).encode())

    def test_cpms_validates_monotonic(self):
        values = [0.094 + i * .005 for i in range(101)]
        parsed = parse_cpms(("var cpms = " + json.dumps(values) + ";").encode())
        self.assertEqual(101, len(parsed))
        values[5] = values[4]
        with self.assertRaises(ValueError):
            parse_cpms(("var cpms = " + json.dumps(values) + ";").encode())


if __name__ == "__main__":
    unittest.main()

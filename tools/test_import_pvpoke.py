import json
import unittest

from import_pvpoke import git_blob_sha1, parse_cpms, parse_pokemon


class ImportTests(unittest.TestCase):
    def pokemon_fixture(self):
        return [
            {"speciesId": f"test_{i}", "speciesName": f"Test {i}",
             "baseStats": {"atk": 120, "def": 115, "hp": 140}}
            for i in range(1501)
        ]

    def test_blob_sha(self):
        # Git's canonical empty blob.
        self.assertEqual(git_blob_sha1(b""), "e69de29bb2d1d6434b8b29ae775ad8c2e48c5391")

    def test_cpms_reject_short_table(self):
        with self.assertRaises(ValueError):
            parse_cpms(b"var cpms = [0.094, 0.135];")

    def test_master_reject_short_list(self):
        with self.assertRaises(ValueError):
            parse_pokemon(json.dumps({"pokemon": []}).encode())

    def test_valid_master_preserves_forms_and_excludes_megas(self):
        data = self.pokemon_fixture()
        data[0]["speciesId"] = "diglett_alolan"
        data[0]["speciesName"] = "Diglett (Alolan)"
        data[1]["speciesId"] = "diglett"
        data.append({"speciesId": "mega_fake", "speciesName": "Mega Fake",
                     "tags": ["mega"],
                     "baseStats": {"atk": 300, "def": 300, "hp": 300}})
        imported = parse_pokemon(json.dumps({"pokemon": data}).encode())
        self.assertEqual(1501, len(imported))
        self.assertEqual("Diglett (Alolan)", imported[0]["name"])
        self.assertEqual(120, imported[0]["base"]["attack"])
        self.assertFalse(any(x["speciesId"] == "mega_fake" for x in imported))

    def test_duplicate_species_id_fails(self):
        data = self.pokemon_fixture()
        data[1]["speciesId"] = data[0]["speciesId"]
        with self.assertRaises(ValueError):
            parse_pokemon(json.dumps({"pokemon": data}).encode())

    def test_invalid_stat_fails(self):
        data = self.pokemon_fixture()
        data[0]["baseStats"]["hp"] = 0
        with self.assertRaises(ValueError):
            parse_pokemon(json.dumps({"pokemon": data}).encode())

    def test_cpms_validates_monotonic(self):
        values = [0.094 + i * .005 for i in range(101)]
        parsed = parse_cpms(("var cpms = " + json.dumps(values) + ";").encode())
        self.assertEqual(101, len(parsed))
        values[5] = values[4]
        with self.assertRaises(ValueError):
            parse_cpms(("var cpms = " + json.dumps(values) + ";").encode())


if __name__ == "__main__":
    unittest.main()

import json


class DataIO:
    """
    JSON input/output
    """

    def load_json(path=None):
        with open(path) as f:
            return json.load(f)

    def save_json(data, path=None):
        with open(path, 'w') as f:
            f.write(json.dumps(data, indent=4))
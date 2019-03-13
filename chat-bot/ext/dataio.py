import json


class DataIO:
    """
    JSON input/output
    """

    def __init__(self):
        pass

    def load_json(self, path=None):
        with open(path) as f:
            return json.load(f)

    def save_json(self, data, path=None):
        with open(path, 'w') as f:
            f.write(json.dumps(data, indent=4))

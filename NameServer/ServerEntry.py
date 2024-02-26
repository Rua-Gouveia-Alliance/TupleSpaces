class ServerEntry:
    def __init__(self, target: str, qualifier: str):
        # host:port combination
        self.target = target
        self.qualifier = qualifier
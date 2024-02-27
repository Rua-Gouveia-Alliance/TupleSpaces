class ServerEntry:
    """
    Stores information about a server (host:port and qualifier)
    """
    def __init__(self, target: str, qualifier: str) -> None:
        self.target = target  # host:port
        self.qualifier = qualifier

    def toString(self) -> str:
        return "host:port -> " + self.target + "\nqualifier ->  " + self.qualifier

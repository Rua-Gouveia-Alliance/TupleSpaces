class ServerEntry:
    """
    Stores information about a server (host:port and qualifier)
    """
    def __init__(self, addr: str, qualifier: str) -> None:
        if qualifier == "":
            raise Exception("Qualifier must be provided")

        self.addr = addr  # host:port
        self.qualifier = qualifier

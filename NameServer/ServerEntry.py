from exceptions.ServerCreationException import ServerCreationException

class ServerEntry:
    """
    Stores information about a server (host:port and qualifier)
    """
    def __init__(self, addr: str, qualifier: str) -> None:
        if qualifier == "":
            raise ServerCreationException("Qualifier must be provided")
        
        # separating ip from port number
        decomposed_addr = addr.split(":")
        if len(decomposed_addr) != 2:
            raise ServerCreationException("Invalid address provided")
        
        try:
            # this raises an exception if the port number is not an integer
            port = int(decomposed_addr[1])
        except:
            raise ServerCreationException("Invalid port number provided")
        
        if port < 1024 or port > 65535:
            raise ServerCreationException("Invalid port number provided")

        self.addr = addr  # host:port
        self.qualifier = qualifier

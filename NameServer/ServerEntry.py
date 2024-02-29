import ipaddress

class ServerEntry:
    """
    Stores information about a server (host:port and qualifier)
    """
    def __init__(self, addr: str, qualifier: str) -> None:
        if qualifier == "":
            raise Exception("Qualifier must be provided")
        
        # separating ip from port number
        decomposed_addr = addr.split(":")
        if len(decomposed_addr) != 2:
            raise Exception("Invalid address provided")
        
        # this raises an exception if the port number is not an integer
        port = int(decomposed_addr[1])
        
        if port < 1024 or port > 65535:
            raise Exception("Invalid port number provided")

        # this raises an exception if the provided ip is invalid and is not localhost
        if decomposed_addr[0] != "localhost":
            ipaddress.ip_address(decomposed_addr[0])

        self.addr = addr  # host:port
        self.qualifier = qualifier

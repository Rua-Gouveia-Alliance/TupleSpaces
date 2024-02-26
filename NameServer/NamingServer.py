from ServiceEntry import ServiceEntry


class NamingServer:
    """
    Keeps all the information the server needs, with a map associating service names
    to their respective ServiceEntry objects
    """
    def __init__(self) -> None:
        # Dictionary that maps service names to entries
        self.services = {}

    def add_service(self, service: str) -> None:
        self.services[service.name] = service

    def get_service(self, service: str) -> ServiceEntry:
        return self.services[service]

    def is_registered(self, service: str) -> bool:
        return service in self.services

from ServiceEntry import ServiceEntry
from ServerEntry import ServerEntry


class NameServer:
    """
    Keeps all the information the server needs, with a map associating service names
    to their respective ServiceEntry objects
    """
    def __init__(self) -> None:
        # Dictionary that maps service names to entries
        self.services = {}

    def add_service(self, service_name: str) -> None:
        self.services[service_name] = ServiceEntry(service_name)

    def get_service(self, service_name: str) -> ServiceEntry:
        return self.services[service_name]

    def is_registered(self, service_name: str) -> bool:
        return service_name in self.services

    def add_server_to_service(self, service_name: str, server: ServerEntry) -> None:
        if not self.is_registered(service_name):
            self.add_service(service_name)
        self.get_service(service_name).add_provider(server)
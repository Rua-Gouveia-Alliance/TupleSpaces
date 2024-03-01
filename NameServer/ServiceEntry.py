import logging

from ServerEntry import ServerEntry


class ServiceEntry:
    """
    Stores a list of ServerEntry objects associated with the name of a service
    """
    def __init__(self, name: str) -> None:
        # This service's name
        self.name = name
        # Servers that provide this service
        self.providers = {}

    def add_provider(self, provider: ServerEntry) -> None:
        # If the provider's identifier already exists it's entry is simply replaced
        self.providers[provider.qualifier] = provider
        logging.debug("Registered provider: " + provider.addr)

    def lookup(self, qualifier: str) -> list: 
        if qualifier == "":
            return list(map(lambda x: x.addr, self.providers.values()))
        if qualifier in self.providers:
            return [self.providers[qualifier].addr]
        return []

    def remove_provider(self, addr: str) -> bool:
        for qualifier in self.providers:
            if self.providers[qualifier].addr == addr:
                del self.providers[qualifier]
                logging.debug("Removed provider: " + addr)
                return True
        # Provider not found
        return False
            
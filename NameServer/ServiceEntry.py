from ServerEntry import ServerEntry


class ServiceEntry:
    """
    Stores a list of ServerEntry objects associated with the name of a service
    """
    def __init__(self, name: str) -> None:
        # This service's name
        self.name = name
        # Servers that provide this service
        self.providers = []

    def add_provider(self, provider: ServerEntry) -> None:
        self.providers.append(provider)

from ServerEntry import ServerEntry

class ServiceEntry:
    def __init__(self, name: str) -> None:
        # This service's name
        self.name = name
        # Servers that provide this service
        self.providers = []
    
    def addProvider(self, provider: ServerEntry) -> None:
        self.providers.append(provider)
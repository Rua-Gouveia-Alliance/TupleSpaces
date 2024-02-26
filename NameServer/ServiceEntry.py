from ServerEntry import ServerEntry

class ServiceEntry:
    def __init__(self, name: str, providers: list[ServerEntry]):
        # This service's name
        self.name = name
        # Servers that provide this service
        self.providers = providers
    
    def addProvider(self, provider: ServerEntry):
        self.providers.append(provider)
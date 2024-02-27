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

    def lookup(self, qualifier) -> list: 
        if qualifier == None:
            return self.providers
        requestedServers = []
        for provider in self.providers:
            if provider.qualifier == qualifier:
                requestedServers.append(provider.toString())
        return requestedServers

    def remove_provider(self, target_address):
        for provider in self.providers:
            if provider.target == target_address:
                self.providers.remove(target_address)
                return target_address
        return None     # TODO Quando o servidor não está associado ao serviço, deve-se retornar o quê?
            
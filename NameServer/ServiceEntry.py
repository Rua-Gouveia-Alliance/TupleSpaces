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

    def lookup(self, qualifier: str) -> list: 
        if qualifier == None:
            return list(map(lambda x: x.target, self.providers))

        requestedServers = []
        for provider in self.providers:
            if provider.qualifier == qualifier:
                requestedServers.append(provider.target)

        return requestedServers

    def remove_provider(self, target_address: str) -> str:
        for i in range(len(self.providers)):
            if self.providers[i].target == target_address:
                self.providers.pop(i)
                return target_address
        return None     # TODO Quando o servidor não está associado ao serviço, deve-se retornar o quê?
            
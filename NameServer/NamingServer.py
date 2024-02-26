from ServiceEntry import ServiceEntry

class NamingServer:
    def __init__(self) -> None:
        # Dictionary that maps service names to entries
        self.services = {}
    
    def addService(self, service: str) -> None:
        self.services[service.name] = service
        
    def getService(self, service: str) -> ServiceEntry:
        return self.services[service]
        
    def isRegistered(self, service: str) -> bool:
        return service in self.services
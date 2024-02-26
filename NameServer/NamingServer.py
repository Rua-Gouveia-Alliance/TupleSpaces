class NamingServer:
    def __init__(self, services):
        # Dictionary that maps service names to entries
        self.services = services
    
    def addService(self, service):
        self.services[service.name] = service
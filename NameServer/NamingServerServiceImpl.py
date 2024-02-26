import sys
import NamingServer
import ServerEntry
import ServiceEntry
sys.path.insert(1, '../contract/target/generated-sources/protobuf/python')
import NamingServer_pb2 as pb2
import NamingServer_pb2_grpc as pb2_grpc

class NamingServerServiceImpl(pb2_grpc.NamingServerServiceImpl):

    def __init__(self,  *args, **kwargs):
        # Instatiating a new naming server
        self.namingServer = NamingServer()
    
    def register(self, request, context):
        # TODO "uma exceção com a descrição Not possible to register the server quando não for possível registar o serviço"
        server = ServerEntry(request.addr, request.qualifier)
        
        # Service already exists, simply add a new provider
        if self.namingServer.isRegistered(request.service):
            self.namingServer.getService(request.service).addProvider(server)
        else: # Service does not yet exist
            service = ServiceEntry()
            service.addProvider(server)
            self.namingServer.addService(service)

        response = pb2.RegisterResponse()
        return response

    def lookup(self, request, context):
        pass

    def delete(self, request, context):
        pass

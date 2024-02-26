import sys
from NamingServer import NamingServer
from ServerEntry import ServerEntry
from ServiceEntry import ServiceEntry

sys.path.insert(1, '../contract/target/generated-sources/protobuf/python')
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc


class NamingServerServiceImpl(pb2_grpc.NameServerServicer):

    def __init__(self, *args, **kwargs):
        # Instantiating a new naming server
        self.namingServer = NamingServer()

    def register(self, request, context):
        # TODO exceção com a descrição "Not possible to register the server" quando não for possível registar o serviço
        server = ServerEntry(request.addr, request.qualifier)

        # Service already exists, simply add a new provider
        if self.namingServer.is_registered(request.service):
            self.namingServer.get_service(request.service).add_provider(server)
        else:  # Service does not yet exist
            service = ServiceEntry()
            service.add_provider(server)
            self.namingServer.add_service(service)

        response = pb2.RegisterResponse()
        return response

    def lookup(self, request, context):
        pass

    def delete(self, request, context):
        pass

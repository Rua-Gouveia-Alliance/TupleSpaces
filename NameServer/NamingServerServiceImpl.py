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
        self.namingServer.add_server_to_service(request.service, server)

        response = pb2.RegisterResponse()
        return response

    def lookup(self, request, context):
        pass

    def delete(self, request, context):
        pass

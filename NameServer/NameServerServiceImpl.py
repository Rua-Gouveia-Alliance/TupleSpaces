import sys
from NameServer import NameServer
from ServerEntry import ServerEntry
from ServiceEntry import ServiceEntry

sys.path.insert(1, '../contract/target/generated-sources/protobuf/python')
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc


class NameServerServiceImpl(pb2_grpc.NameServerServicer):

    def __init__(self, *args, **kwargs):
        # Instantiating a new name server
        self.nameServer = NameServer()

    def register(self, request, context):
        # TODO exceção com a descrição "Not possible to register the server" quando não for possível registar o servidor
        server = ServerEntry(request.addr, request.qualifier)
        self.nameServer.add_server_to_service(request.service, server)

        response = pb2.RegisterResponse()
        return response

    def lookup(self, request, context):
        if self.nameServer.is_registered(request.service):     # TODO quando service não existe, retornar lista vazia
            service = self.nameServer.get_service(request.service)
            requestedServers = service.lookup(request.qualifier)
        else:
            requestedServers = []

        response = pb2.LookupResponse(requestedServers)
        return response


    def delete(self, request, context):
        # TODO exceção com a descrição "Not possible to remove the server" quando não for possível remover o servidor
        server = ServerEntry(request.addr, request.qualifier)
        self.nameServer.add_server_to_service(request.service, server)

        response = pb2.RegisterResponse()
        return response

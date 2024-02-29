import sys
from NameServer import NameServer
from ServerEntry import ServerEntry
from ServiceEntry import ServiceEntry
from exceptions.ServerCreationException import ServerCreationException
from exceptions.ServerDeletionException import ServerDeletionException

sys.path.insert(1, "../contract/target/generated-sources/protobuf/python")
import grpc
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc


class NameServerServiceImpl(pb2_grpc.NameServerServicer):

    def __init__(self, *args, **kwargs):
        # Instantiating a new name server
        self.nameServer = NameServer()

    def register(self, request, context):
        try:
            server = ServerEntry(request.addr, request.qualifier)
            self.nameServer.add_server_to_service(request.service, server)
        except ServerCreationException as exception:
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details(str(exception))
        #TODO perguntar ao professor se faz sentido falhar na criação quando um qualifier não é dado

        return pb2.RegisterResponse()

    def lookup(self, request, context):
        if self.nameServer.is_registered(request.service):
            service = self.nameServer.get_service(request.service)
            requestedServers = service.lookup(request.qualifier)
        else:
            requestedServers = []

        response = pb2.LookupResponse()
        response.servers[:] = requestedServers
        return response

    def delete(self, request, context):
        try:
            self.nameServer.remove_server_from_service(request.service, request.addr)
        except ServerDeletionException as exception:
            context.set_code(grpc.StatusCode.FAILED_PRECONDITION)
            context.set_details(str(exception))
        return pb2.DeleteResponse()

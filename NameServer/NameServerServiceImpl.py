import logging
import sys

from NameServer import NameServer
from ServerEntry import ServerEntry
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
            logging.debug("Received register request with service %s, address %s and qualifier %s",
                          request.service, request.addr, request.qualifier)
            server = ServerEntry(request.addr, request.qualifier)
            self.nameServer.add_server_to_service(request.service, server)
        except ServerCreationException as exception:
            logging.debug("Sending exception %s", str(exception))
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details(str(exception))

        logging.debug("Sending register response")
        return pb2.RegisterResponse()

    def lookup(self, request, context):
        logging.debug("Received lookup request with service %s and qualifier %s",
                      request.service, request.qualifier)
        if self.nameServer.is_registered(request.service):
            service = self.nameServer.get_service(request.service)
            requested_servers = service.lookup(request.qualifier)
        else:
            requested_servers = []

        response = pb2.LookupResponse()
        response.servers[:] = requested_servers
        logging.debug("Sending lookup response:" + ', '.join(server for server in requested_servers))
        return response

    def delete(self, request, context):
        logging.debug("Received lookup request with service %s, address %s",
                      request.service, request.addr)
        try:
            self.nameServer.remove_server_from_service(request.service, request.addr)
        except ServerDeletionException as exception:
            logging.debug("Sending exception %s", str(exception))
            context.set_code(grpc.StatusCode.FAILED_PRECONDITION)
            context.set_details(str(exception))
        logging.debug("Sending delete response")
        return pb2.DeleteResponse()

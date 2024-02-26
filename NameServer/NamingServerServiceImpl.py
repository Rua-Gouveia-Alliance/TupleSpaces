import sys
import NamingServer
sys.path.insert(1, '../contract/target/generated-sources/protobuf/python')
import NamingServer_pb2 as pb2
import NamingServer_pb2_grpc as pb2_grpc

class NamingServerServiceImpl(pb2_grpc.NamingServerServiceImpl):

    def __init__(self,  *args, **kwargs):
        # Instatiating a new naming server
        self.server = NamingServer()
    
    def register(self, request, context):
        pass

    def lookup(self, request, context):
        pass

    def delete(self, request, context):
        pass

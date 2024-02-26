import sys
sys.path.insert(1, "../Contract/target/generated-sources/protobuf/python")
import grpc
import NamingServer_pb2 as pb2
import NamingServer_pb2_grpc as pb2_grpc
from NamingServerServiceImpl import NamingServerServiceImpl
from concurrent import futures

# define the port
PORT = 5001

if __name__ == "__main__":
    try:
        # print received arguments
        print("Received arguments:")
        for i in range(1, len(sys.argv)):
            print("  " + sys.argv[i])

        # create server
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))

        # add service
        pb2_grpc.add_NamingServerServiceServicer_to_server(NamingServerServiceImpl(), server)

        # listen on port
        server.add_insecure_port("[::]:" + str(PORT))

        # start server
        server.start()

        # wait for server to finish
        server.wait_for_termination()

    except KeyboardInterrupt:
        print("DNS server stopped")
        exit(0)

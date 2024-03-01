import logging, sys
import argparse
sys.path.insert(1, "../Contract/target/generated-sources/protobuf/python")
import grpc
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
from NameServerServiceImpl import NameServerServiceImpl
from concurrent import futures

# define the port
PORT = 5001

if __name__ == "__main__":
    parser = argparse.ArgumentParser(prog="NameServer",description="Store servers by name given") 
    parser.add_argument("-debug", action="store_true", default=False, help="Show messages for debugging purposes")
    args = parser.parse_args()

    if args.debug:
        logging.basicConfig(level=logging.DEBUG)
    else:
        logging.basicConfig(level=logging.CRITICAL)

    logger = logging.getLogger()
    logger_handler = logging.StreamHandler()
    formatter = logging.Formatter('%(levelname)s- %(message)s')
    logger_handler.setFormatter(formatter)
    logger.handlers = []
    logger.addHandler(logger_handler)

    #logger_handler.setFormatter(logging.Formatter("%(message)s"))

    try:
        # print received arguments
        logging.debug("Received arguments:")
        for i in range(1, len(sys.argv)):
           logging.debug("  " + sys.argv[i])

        # create server
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))

        # add service
        pb2_grpc.add_NameServerServicer_to_server(NameServerServiceImpl(), server)

        # listen on port
        server.add_insecure_port("[::]:" + str(PORT))

        # start server
        server.start()

        # wait for server to finish
        server.wait_for_termination()

    except KeyboardInterrupt:
        print("DNS server stopped")
        exit(0)

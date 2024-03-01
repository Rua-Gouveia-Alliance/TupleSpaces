import argparse
import logging
import sys

sys.path.insert(1, "../Contract/target/generated-sources/protobuf/python")
import grpc
import NameServer_pb2_grpc as pb2_grpc
from NameServerServiceImpl import NameServerServiceImpl
from concurrent import futures

# define the port
PORT = 5001


def configure_logger():
    # get debug argument
    parser = argparse.ArgumentParser(prog="NameServer", description="Store servers by name given")
    parser.add_argument("-debug", action="store_true", default=False, help="Show messages for debugging purposes")
    args = parser.parse_args()

    # configure logging levels
    if args.debug:
        logging.basicConfig(level=logging.DEBUG)  # -debug
    else:
        logging.basicConfig(level=logging.CRITICAL)  # default

    logger = logging.getLogger()
    logger.handlers = []  # delete default handler

    # configure new handler with custom message format
    logger_handler = logging.StreamHandler()
    logger_handler.setFormatter(logging.Formatter('[%(levelname)s] %(message)s'))
    logger.addHandler(logger_handler)


if __name__ == "__main__":
    try:
        print("Starting NameServer...")

        # configure debug messages
        configure_logger()

        # create server
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))

        # add service
        pb2_grpc.add_NameServerServicer_to_server(NameServerServiceImpl(), server)

        # listen on port
        server.add_insecure_port("[::]:" + str(PORT))

        # start server
        server.start()
        logging.debug("Server started. Listening on port " + str(PORT))

        # wait for server to finish
        server.wait_for_termination()

    except KeyboardInterrupt:
        print("Stopping NameServer...")
        exit(0)

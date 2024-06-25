import sys
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')
# define the port
PORT = 5001

import grpc
from concurrent import futures
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc

from NamingServer import NamingServer
from NamingServerServiceImpl import NamingServerServiceImpl
from ServerEntry import ServerEntry
from ServiceEntry import ServiceEntry

if __name__ == '__main__':
    try:
        # print received arguments
        print("Received arguments:")
        for i in range(1, len(sys.argv)):
            print("  " + sys.argv[i])

        naming_server = NamingServer()

        naming_impl = NamingServerServiceImpl(naming_server)
        # create server
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
        # add service
        pb2_grpc.add_NamingServerServiceServicer_to_server(naming_impl, server)
        # listen on port
        server.add_insecure_port('[::]:'+ str(PORT))
        # start server
        server.start()
        # print message
        print("Naming Server listening on port " + str(PORT))
        # print termination message
        print("Press CTRL+C to terminate")
        # wait for server to finish
        server.wait_for_termination()

    except KeyboardInterrupt:
        print("NamingServer stopped")
        exit(0)

from typing import List
import sys
import grpc

sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
from NameServer_pb2 import LookupResponse, RegisterResponse, DeleteResponse
from NamingServer import NamingServer


class NamingServerServiceImpl(pb2_grpc.NamingServerServiceServicer):

    def __init__(self, server: NamingServer):
        self.server = server

    def lookup(self, request, context):
        response = LookupResponse(server=[pb2.Server(serverTarget=entry.get_target(), qualifier=entry.get_qualifier())
                                          for entry in self.server.lookup(request.serviceName, request.qualifier)])
        return response

    def register(self, request, context):
        result = self.server.register(request.serviceName, request.qualifier, request.serverAddress)
        if result == NamingServer.NamingServerResult.SERVER_ALREADY_REGISTERED:
            context.set_details("Not possible to register the server")
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            return RegisterResponse()
        return RegisterResponse()

    def delete(self, request, context):
        result = self.server.delete(request.serviceName, request.target)
        if result == NamingServer.NamingServerResult.SERVICE_NOT_FOUND:
            context.set_details("Not possible to remove the server")
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            return DeleteResponse()
        return DeleteResponse()

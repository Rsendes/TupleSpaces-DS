from typing import Dict, List
from ServerEntry import ServerEntry
from ServiceEntry import ServiceEntry
import sys
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')

class NamingServer:
    class NamingServerResult:
        OK = "OK"
        SERVICE_NOT_FOUND = "SERVICE_NOT_FOUND"
        SERVER_ALREADY_REGISTERED = "SERVER_ALREADY_REGISTERED"

    def __init__(self):
        self.services: Dict[str, ServiceEntry] = {}

    def get_services(self) -> Dict[str, ServiceEntry]:
        return self.services.copy()

    def add_service(self, service_name: str, service_entry: ServiceEntry) -> None:
        self.services[service_name] = service_entry

    def lookup(self, service: str, qualifier: str) -> List[ServerEntry]:
        if service not in self.services:
            return []
        if service in self.services and not qualifier:
            return self.services[service].get_server_entry_list()
        return [server_entry for server_entry in self.services[service].get_server_entry_list() if server_entry.get_qualifier() == qualifier]

    def register(self, service_name: str, qualifier: str, server_address: str) -> str:
        if service_name in self.services:
            service_entry = self.services[service_name]
        else:
            service_entry = ServiceEntry(service_name)
            self.add_service(service_name, service_entry)

        if service_entry.check_server_entry_exists(server_address, qualifier):
            return self.NamingServerResult.SERVER_ALREADY_REGISTERED
        else:
            service_entry.add_server_entry(ServerEntry(server_address, qualifier))
            return self.NamingServerResult.OK

    def delete(self, service_name: str, target: str) -> str:
        if service_name not in self.services:
            return self.NamingServerResult.SERVICE_NOT_FOUND
        for entry in self.services[service_name].get_server_entry_list():
            if entry.get_target() == target:
                self.services[service_name].get_server_entry_list().remove(entry)
        return self.NamingServerResult.OK

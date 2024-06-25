from typing import List
from ServerEntry import ServerEntry
import sys
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')


class ServiceEntry:
    def __init__(self, service_name: str):
        self.service_name = service_name
        self.server_entry_list: List[ServerEntry] = []

    def get_server_entry_list(self) -> List['ServerEntry']:
        return self.server_entry_list

    def check_server_entry_exists(self, target: str, qualifier: str) -> bool:
        return any(entry.get_target() == target and entry.get_qualifier() == qualifier for entry in self.server_entry_list)

    def set_server_entry_list(self, server_entry_list: List['ServerEntry']) -> None:
        self.server_entry_list = server_entry_list

    def get_service_name(self) -> str:
        return self.service_name

    def set_service_name(self, service_name: str) -> None:
        self.service_name = service_name

    def add_server_entry(self, server_entry: 'ServerEntry') -> None:
        self.server_entry_list.append(server_entry)

    def remove_server_entry(self, server_entry: 'ServerEntry') -> None:
        self.server_entry_list.remove(server_entry)
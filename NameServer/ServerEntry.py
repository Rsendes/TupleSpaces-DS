import sys
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')

class ServerEntry:
    def __init__(self, target, qualifier):
        self.target = target
        self.qualifier = qualifier
    def get_target(self):
        return self.target

    def set_target(self, target):
        self.target = target

    def get_qualifier(self):
        return self.qualifier

    def set_qualifier(self, qualifier):
        self.qualifier = qualifier









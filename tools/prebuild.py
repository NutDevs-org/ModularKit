"""Replace version in pom.xml for server backups."""
import sys
import xml.etree.ElementTree as Et


def get_root(file_path: str) -> Et.Element:
    """Retrieve xml root element from pom."""
    return Et.parse(file_path).getroot()


def save_pom(file_path: str, tree_root: Et.Element) -> None:
    """Cleanup xml and save it."""
    content: str = Et.tostring(tree_root).decode()
    clean: str = content.replace("ns0:", '').replace(":ns0", '')

    with open(file_path, 'w+') as f:
        f.write(clean)


def get_commit_id() -> str:
    """Retrieve the repository id."""
    return sys.argv[1]


def main() -> None:
    print(sys.argv[2])
    file: str = 'pom.xml'
    file_dest: str = ".buildconfig-pom.xml"

    xml_root = get_root(file)

    if sys.argv[2] == "devel":
        xml_root[3].text += f"-nightly_{get_commit_id()}"


    """ Remove GPG-Sign Maven Plugin if not a release build. """


    if not sys.argv[2] == "release":
        xml_root[13][1][5].clear()
        xml_root[13][1].remove(xml_root[13][1][5])


    save_pom(file_dest, xml_root)


if __name__ == '__main__':
    main()

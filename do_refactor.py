import os
import glob
import re
import shutil

src_dir = 'src/main/java'
base_old = 'com.b4code.backend'
base_new = 'com.hospitality'

overrides = {
    'com.b4code.backend.modules.guest.models.Property': ('com.hospitality.models', 'GuestProperty.java'),
    'com.b4code.backend.modules.guest.dao.PropertyRepository': ('com.hospitality.dao', 'GuestPropertyRepository.java'),
    'com.b4code.backend.modules.admin.service.UserService': ('com.hospitality.service', 'AdminUserService.java'),
    'com.b4code.backend.modules.admin.service.impl.UserServiceImpl': ('com.hospitality.service.impl', 'AdminUserServiceImpl.java'),
}

class_name_mapping = {}
file_moves = [] # list of dicts

def determine_module(old_pkg, file_name):
    if 'admin' in old_pkg: return 'admin'
    if 'auth' in old_pkg: return 'auth'
    if 'payment' in old_pkg: return 'payment'
    if 'qr' in old_pkg: return 'property'
    if 'staff' in old_pkg: return 'foodorder'
    if 'user' in old_pkg: return 'auth'
    if 'guest' in old_pkg:
        lower_name = file_name.lower()
        if 'book' in lower_name or 'room' in lower_name: return 'booking'
        if 'review' in lower_name: return 'review'
        if 'message' in lower_name: return 'messaging'
        return 'property'
    return 'common'

def determine_new_package(old_pkg, file_name, content):
    lower_name = file_name.lower()
    if 'config' in old_pkg or lower_name.endswith('config.java') or lower_name == 'dataseeder.java': return f'{base_new}.config'
    if 'security' in old_pkg or 'jwt' in lower_name or 'userdetails' in lower_name or 'authentication' in lower_name or lower_name == 'securityutils.java': return f'{base_new}.security'
    if lower_name == 'corsconfig.java': return f'{base_new}.config'
    if 'repository' in lower_name or 'dao' in old_pkg: return f'{base_new}.dao'
    if 'entity' in old_pkg or 'models' in old_pkg or '@Entity' in content: return f'{base_new}.models'
    if 'dto' in old_pkg or lower_name.endswith('request.java') or (lower_name.endswith('response.java') and 'apiresponse' not in lower_name and 'errorresponse' not in lower_name and 'paginationresponse' not in lower_name):
        return f'{base_new}.dto.{determine_module(old_pkg, file_name)}'
    if 'rest' in old_pkg or 'controller' in lower_name:
        return f'{base_new}.rest.{determine_module(old_pkg, file_name)}'
    if 'serviceimpl' in lower_name or 'service.impl' in old_pkg: return f'{base_new}.service.impl'
    if 'service' in lower_name or 'service' in old_pkg or 'seeder' in lower_name: return f'{base_new}.service'
    if 'util' in old_pkg or lower_name.endswith('util.java'): return f'{base_new}.util'
    if 'exception' in old_pkg or lower_name.endswith('exception.java') or lower_name.endswith('exceptionhandler.java'): return f'{base_new}.exceptions'
    if lower_name in ['apiresponse.java', 'errorresponse.java', 'paginationresponse.java'] or 'response' in old_pkg: return f'{base_new}.response'
    if 'scheduler' in old_pkg or lower_name.endswith('scheduler.java'): return f'{base_new}.scheduler'
    if 'enum' in old_pkg: return f'{base_new}.enums'
    if file_name == 'B4CodeBackendApplication.java': return base_new
    return base_new + '.common'

# Phase 1: Scan and map
java_files = glob.glob(os.path.join(src_dir, '**', '*.java'), recursive=True)

for file_path in java_files:
    file_path = file_path.replace('\\', '/')
    file_name = os.path.basename(file_path)
    with open(file_path, 'r', encoding='utf-8') as f: content = f.read()
    pkg_match = re.search(r'package\s+([a-zA-Z0-9_.]+);', content)
    if not pkg_match: continue
    old_pkg = pkg_match.group(1)
    old_fqcn = f"{old_pkg}.{file_name.replace('.java', '')}"
    new_pkg = determine_new_package(old_pkg, file_name, content)
    new_file_name = file_name
    if file_name == 'B4CodeBackendApplication.java': new_file_name = 'HospitalityPlatformApplication.java'
    if old_fqcn in overrides: new_pkg, new_file_name = overrides[old_fqcn]
    new_fqcn = f"{new_pkg}.{new_file_name.replace('.java', '')}"
    new_path = os.path.join(src_dir, *new_pkg.split('.'), new_file_name).replace('\\', '/')
    
    class_name_mapping[old_fqcn] = new_fqcn
    file_moves.append({
        'old_path': file_path, 'new_path': new_path,
        'old_pkg': old_pkg, 'new_pkg': new_pkg,
        'old_fqcn': old_fqcn, 'new_fqcn': new_fqcn,
        'old_file_name': file_name, 'new_file_name': new_file_name,
        'content': content
    })

# Mappings for internal renaming
rename_mappings = {
    'com.hospitality.models.GuestProperty': ('Property', 'GuestProperty'),
    'com.hospitality.dao.GuestPropertyRepository': ('PropertyRepository', 'GuestPropertyRepository'),
    'com.hospitality.service.AdminUserService': ('UserService', 'AdminUserService'),
    'com.hospitality.service.impl.AdminUserServiceImpl': ('UserServiceImpl', 'AdminUserServiceImpl'),
}

# Phase 2: Refactor content and write to new paths
for move in file_moves:
    content = move['content']
    
    # 1. Update package
    content = re.sub(r'^package\s+' + re.escape(move['old_pkg']) + r';', f"package {move['new_pkg']};", content, flags=re.MULTILINE)
    
    # 2. Update imports
    for old_fqcn, new_fqcn in class_name_mapping.items():
        if old_fqcn != new_fqcn:
            # Replace exact import
            content = re.sub(r'^import\s+' + re.escape(old_fqcn) + r';', f"import {new_fqcn};", content, flags=re.MULTILINE)
            # Replace fully qualified usages in code
            content = content.replace(old_fqcn, new_fqcn)
            
    # 3. Rename usages if this file uses a renamed class
    # Check if the file IS the renamed class, or IMPORTS the renamed class
    for new_fqcn, (old_name, new_name) in rename_mappings.items():
        if move['new_fqcn'] == new_fqcn or f"import {new_fqcn};" in content or (new_fqcn.rsplit('.', 1)[0] == move['new_pkg'] and new_name in content): 
            # It's in the same package or imported, so it might use the old name.
            # But wait! For the file itself, the class name might still be the old one.
            # E.g. public class Property -> public class GuestProperty
            content = re.sub(r'\b' + re.escape(old_name) + r'\b', new_name, content)
            
    # Also handle the application class rename
    if move['old_file_name'] == 'B4CodeBackendApplication.java':
        content = re.sub(r'\bB4CodeBackendApplication\b', 'HospitalityPlatformApplication', content)

    # Make directories and write file
    os.makedirs(os.path.dirname(move['new_path']), exist_ok=True)
    with open(move['new_path'], 'w', encoding='utf-8') as f:
        f.write(content)
        
    # Remove old file
    if os.path.abspath(move['old_path']) != os.path.abspath(move['new_path']):
        os.remove(move['old_path'])

print("Refactoring complete.")

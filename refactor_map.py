import os
import glob
import re
import shutil

src_dir = 'src/main/java'
base_old = 'com.b4code.backend'
base_new = 'com.hospitality'

old_base_path = os.path.join(src_dir, *base_old.split('.'))
new_base_path = os.path.join(src_dir, *base_new.split('.'))

# Explicit Overrides to resolve duplicates
overrides = {
    'com.b4code.backend.modules.guest.models.Property': ('com.hospitality.models', 'GuestProperty.java'),
    'com.b4code.backend.modules.guest.dao.PropertyRepository': ('com.hospitality.dao', 'GuestPropertyRepository.java'),
    'com.b4code.backend.modules.admin.service.UserService': ('com.hospitality.service', 'AdminUserService.java'),
    'com.b4code.backend.modules.admin.service.impl.UserServiceImpl': ('com.hospitality.service.impl', 'AdminUserServiceImpl.java'),
}

# Mappings
# class_name_mapping[old_fqcn] = new_fqcn
class_name_mapping = {}
file_moves = [] # list of (old_path, new_path)

def determine_module(old_pkg, file_name):
    # Try to guess module from old package
    if 'admin' in old_pkg: return 'admin'
    if 'auth' in old_pkg: return 'auth'
    if 'payment' in old_pkg: return 'payment'
    if 'qr' in old_pkg: return 'property' # Or something else? Let's say property for QR
    if 'staff' in old_pkg: return 'foodorder' # Assuming staff handles food order
    if 'user' in old_pkg: return 'auth' # or admin? Let's say auth or admin.

    # guest module mapping
    if 'guest' in old_pkg:
        lower_name = file_name.lower()
        if 'book' in lower_name or 'room' in lower_name: return 'booking'
        if 'review' in lower_name: return 'review'
        if 'message' in lower_name: return 'messaging'
        return 'property'
        
    return 'common'

def determine_new_package(old_pkg, file_name, content):
    lower_name = file_name.lower()
    
    # 1. Config
    if 'config' in old_pkg or lower_name.endswith('config.java') or lower_name == 'dataseeder.java':
        return f'{base_new}.config'
        
    # 2. Security
    if 'security' in old_pkg or 'jwt' in lower_name or 'userdetails' in lower_name or 'authentication' in lower_name or lower_name == 'securityutils.java' or lower_name == 'corsconfig.java':
        return f'{base_new}.security' # But cors config might be better in config.
        
    if lower_name == 'corsconfig.java':
        return f'{base_new}.config'
        
    # 3. DAO
    if 'repository' in lower_name or 'dao' in old_pkg:
        return f'{base_new}.dao'
        
    # 4. Models
    if 'entity' in old_pkg or 'models' in old_pkg or '@Entity' in content:
        return f'{base_new}.models'
        
    # 5. DTO
    if 'dto' in old_pkg or lower_name.endswith('request.java') or (lower_name.endswith('response.java') and 'apiresponse' not in lower_name and 'errorresponse' not in lower_name and 'paginationresponse' not in lower_name):
        module = determine_module(old_pkg, file_name)
        return f'{base_new}.dto.{module}'
        
    # 6. REST
    if 'rest' in old_pkg or 'controller' in lower_name:
        module = determine_module(old_pkg, file_name)
        return f'{base_new}.rest.{module}'
        
    # 7. Service
    if 'serviceimpl' in lower_name or 'service.impl' in old_pkg:
        return f'{base_new}.service.impl'
    if 'service' in lower_name or 'service' in old_pkg or 'seeder' in lower_name:
        return f'{base_new}.service'
        
    # 8. Util
    if 'util' in old_pkg or lower_name.endswith('util.java'):
        return f'{base_new}.util'
        
    # 9. Exceptions
    if 'exception' in old_pkg or lower_name.endswith('exception.java') or lower_name.endswith('exceptionhandler.java'):
        return f'{base_new}.exceptions'
        
    # 10. Response
    if lower_name in ['apiresponse.java', 'errorresponse.java', 'paginationresponse.java'] or 'response' in old_pkg:
        return f'{base_new}.response'
        
    # 11. Scheduler
    if 'scheduler' in old_pkg or lower_name.endswith('scheduler.java'):
        return f'{base_new}.scheduler'
        
    # 12. Enums
    if 'enum' in old_pkg:
        return f'{base_new}.enums'
        
    if file_name == 'B4CodeBackendApplication.java':
        return base_new

    # Fallback
    return base_new + '.common'

# Phase 1: Scan and map
java_files = glob.glob(os.path.join(src_dir, '**', '*.java'), recursive=True)

for file_path in java_files:
    file_path = file_path.replace('\\', '/')
    file_name = os.path.basename(file_path)
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
        
    # Find package
    pkg_match = re.search(r'package\s+([a-zA-Z0-9_.]+);', content)
    if not pkg_match:
        continue
    old_pkg = pkg_match.group(1)
    old_fqcn = f"{old_pkg}.{file_name.replace('.java', '')}"
    
    new_pkg = determine_new_package(old_pkg, file_name, content)
    
    new_file_name = file_name
    if file_name == 'B4CodeBackendApplication.java':
        new_file_name = 'HospitalityPlatformApplication.java'
        
    if old_fqcn in overrides:
        new_pkg, new_file_name = overrides[old_fqcn]
        
    new_fqcn = f"{new_pkg}.{new_file_name.replace('.java', '')}"
    
    new_path = os.path.join(src_dir, *new_pkg.split('.'), new_file_name).replace('\\', '/')
    
    class_name_mapping[old_fqcn] = new_fqcn
    file_moves.append((file_path, new_path, old_pkg, new_pkg, new_file_name))

# Print mapping for review
for old_fqcn, new_fqcn in class_name_mapping.items():
    print(f"{old_fqcn} -> {new_fqcn}")

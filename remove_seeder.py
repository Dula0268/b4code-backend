import re

with open('src/main/java/com/b4code/backend/common/config/DataSeeder.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Remove imports
content = re.sub(r'import com\.b4code\.backend\.modules\.guest\.dao\.\*;\n', '', content)
content = re.sub(r'import com\.b4code\.backend\.modules\.guest\.models\.\*;\n', '', content)
content = re.sub(r'import java\.math\.BigDecimal;\n', '', content)
content = re.sub(r'import java\.time\.LocalDate;\n', '', content)
content = re.sub(r'import java\.time\.LocalDateTime;\n', '', content)
content = re.sub(r'import java\.util\.Arrays;\n', '', content)

# Remove repositories
content = re.sub(r'    private final PropertyRepository propertyRepository;\n', '', content)
content = re.sub(r'    private final RoomRepository roomRepository;\n', '', content)
content = re.sub(r'    private final BookingRepository bookingRepository;\n', '', content)
content = re.sub(r'    private final ReviewRepository reviewRepository;\n', '', content)
content = re.sub(r'    private final MessageRepository messageRepository;\n', '', content)

# Remove guest module seeding block inside run()
guest_seed_block = r'''        // ── Seed guest module data \(properties, rooms, bookings, reviews, messages\) ─────
        // Always empty the tables first to start fresh and apply new image URLs
        messageRepository\.deleteAll\(\);
        reviewRepository\.deleteAll\(\);
        bookingRepository\.deleteAll\(\);
        roomRepository\.deleteAll\(\);
        propertyRepository\.deleteAll\(\);
        
        seedGuestData\(\);'''
content = re.sub(guest_seed_block, '', content)

# Remove seedGuestData method
# Find start of seedGuestData
start_idx = content.find('    private void seedGuestData() {')
if start_idx != -1:
    content = content[:start_idx]

# Ensure we close the class properly if we removed seedGuestData at the end
if start_idx != -1:
    content = content.rstrip() + '\n}\n'

with open('src/main/java/com/b4code/backend/common/config/DataSeeder.java', 'w', encoding='utf-8') as f:
    f.write(content)

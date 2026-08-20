-- Physical room number staff assign at check-in, distinct from the booked
-- room TYPE (guest.bookings.room_id references room_types, a category like
-- "Standard Room" — this is the actual numbered room, e.g. "101").
ALTER TABLE guest.bookings ADD COLUMN IF NOT EXISTS room_number VARCHAR(50);

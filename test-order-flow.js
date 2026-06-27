const http = require('http');

function request(options, data) {
    return new Promise((resolve, reject) => {
        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    try {
                        resolve(JSON.parse(body || '{}'));
                    } catch (e) {
                        resolve(body);
                    }
                } else {
                    reject(`Request failed with status ${res.statusCode}: ${body}`);
                }
            });
        });
        req.on('error', reject);
        if (data) {
            req.write(JSON.stringify(data));
        }
        req.end();
    });
}

async function runTest() {
    try {
        console.log("=== Testing Order Flow ===");
        
        // 0. Fetch a valid menu item
        console.log("0. Fetching valid menu items...");
        const menuItems = await request({
            hostname: 'localhost',
            port: 8080,
            path: '/api/menu-items/property/1',
            method: 'GET'
        });
        
        if (!menuItems || menuItems.length === 0) {
            console.log("No menu items found! Creating a dummy menu item...");
            // Optionally, create one, but for now just fail or use hardcoded if we can't create
            throw new Error("No menu items found. Please add a menu item first.");
        }
        
        const validMenuItemId = menuItems[0].id;
        console.log("Found valid menuItemId:", validMenuItemId);

        // 1. Guest places an order with 2 items and a custom instruction
        console.log("\n1. Guest placing order...");
        const orderPayload = {
            propertyId: 1,
            guestId: 1,
            location: "Room 101",
            guestName: "John Doe",
            guestPhone: "1234567890",
            guestInstructions: "No onions please",
            paymentMethod: "room-charge",
            totalAmount: 2500,
            status: "NEW",
            items: [
                { menuItemId: validMenuItemId, quantity: 1, priceAtOrder: 1500 },
                { menuItemId: validMenuItemId, quantity: 2, priceAtOrder: 500 }
            ]
        };

        const createRes = await request({
            hostname: 'localhost',
            port: 8080,
            path: '/api/orders',
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        }, orderPayload);

        console.log("Order created:", createRes);
        const orderId = createRes.id;
        
        if (!orderId) {
            throw new Error("Order ID not returned.");
        }
        
        console.log(`\n2. Staff receives it. Order ID: ${orderId}, Status: ${createRes.status}`);
        console.log(`Custom Instructions: ${createRes.guestInstructions}`);
        
        // 3. Staff accepts it and changes status to PREPARING
        console.log("\n3. Staff accepts order (changes status to PREPARING)...");
        const updateRes = await request({
            hostname: 'localhost',
            port: 8080,
            path: `/api/orders/${orderId}/status?status=PREPARING`,
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' }
        });
        
        console.log("Order updated status:", updateRes.status);
        
        // 4. Guest sees the updated order
        console.log("\n4. Guest checks order status (should be PREPARING)...");
        const getRes = await request({
            hostname: 'localhost',
            port: 8080,
            path: '/api/orders', 
            method: 'GET'
        });
        
        const myOrder = getRes.find(o => o.id === orderId);
        console.log(`Guest sees order status: ${myOrder ? myOrder.status : 'Not found'}`);
        
        if (myOrder && myOrder.status === 'PREPARING') {
            console.log("\n✅ Test passed successfully!");
        } else {
            console.log("\n❌ Test failed: Status not updated correctly.");
        }
        
    } catch (e) {
        console.error("Test Error:", e);
    }
}

runTest();

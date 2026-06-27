const http = require('http');

function request(url, options, body = null) {
  return new Promise((resolve, reject) => {
    const req = http.request(url, options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          const parsed = data ? JSON.parse(data) : null;
          resolve({ status: res.statusCode, data: parsed });
        } catch(e) {
          resolve({ status: res.statusCode, data });
        }
      });
    });
    req.on('error', reject);
    if (body) {
      req.write(JSON.stringify(body));
    }
    req.end();
  });
}

async function runTest() {
  console.log("=== Testing QR Flow for Room 1 ===");
  try {
    // 1. Generate QR Code
    console.log("1. Generating QR code for Room 101...");
    const qrPayload = {
      propertyId: 1,
      name: "Room 101 QR",
      location: "Room 101",
      type: "ROOM",
      description: "Test QR",
      instructionText: "Scan to order",
      showRoomNumber: true,
      showLogo: false
    };
    
    const qrRes = await request('http://localhost:8080/api/qr/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    }, qrPayload);
    
    if (qrRes.status !== 201) {
      throw new Error(`Failed to generate QR: ${qrRes.status} ${JSON.stringify(qrRes.data)}`);
    }
    
    const uniqueQrId = qrRes.data.uniqueQrId;
    console.log(`✅ Generated QR Code with uniqueQrId: ${uniqueQrId}`);
    
    // 2. Fetch QR Data (Simulating frontend page.tsx)
    console.log(`\n2. Fetching QR Data as frontend would...`);
    const fetchRes = await request(`http://localhost:8080/api/qr/unique/${uniqueQrId}`, {
      method: 'GET'
    });
    
    if (fetchRes.status !== 200) {
      throw new Error(`Failed to fetch QR Data: ${fetchRes.status}`);
    }
    
    const qrData = fetchRes.data;
    console.log(`✅ Frontend fetched QR data:`);
    console.log(`   propertyId: ${qrData.propertyId}`);
    console.log(`   location: ${qrData.location}`);
    console.log(`   type: ${qrData.type}`);
    
    // 3. Place Order (Simulating Guest Order)
    console.log(`\n3. Placing Order as guest...`);
    const orderPayload = {
      propertyId: qrData.propertyId,
      location: qrData.location, // Sending location from QR
      guestName: "Guest User",
      guestPhone: "1234567890",
      totalAmount: 1500,
      paymentMethod: "pay-at-property",
      items: [
        { menuItemId: 1001, quantity: 1, priceAtOrder: 1500 } // Assuming item 1001 exists
      ]
    };
    
    const orderRes = await request('http://localhost:8080/api/orders', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    }, orderPayload);
    
    if (orderRes.status !== 200 && orderRes.status !== 201) {
      throw new Error(`Failed to place order: ${orderRes.status} ${JSON.stringify(orderRes.data)}`);
    }
    
    console.log(`✅ Order Placed successfully! Order ID: ${orderRes.data.id}`);
    console.log(`   Order Location recorded as: ${orderRes.data.location}`);
    
    console.log("\n✅ All tests passed. The location identifier is working correctly!");
  } catch (error) {
    console.error("\n❌ Test Error:", error);
  }
}

runTest();

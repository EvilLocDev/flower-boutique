const functions = require("firebase-functions");
const crypto = require("crypto");
const moment = require("moment");
const querystring = require('qs');

// 1. HÀM SẮP XẾP VÀ MÃ HÓA CHUẨN CỦA VNPAY (BẮT BUỘC PHẢI CÓ)
function sortObject(obj) {
    let sorted = {};
    let str = [];
    let key;
    for (key in obj){
        if (obj.hasOwnProperty(key)) {
            str.push(encodeURIComponent(key));
        }
    }
    str.sort();
    for (key = 0; key < str.length; key++) {
        sorted[str[key]] = encodeURIComponent(obj[str[key]]).replace(/%20/g, "+");
    }
    return sorted;
}

exports.createVNPayUrl = functions.https.onRequest((req, res) => {
    // THÔNG TIN TÀI KHOẢN MỚI CỦA BẠN
    const tmnCode = "PEVFJHL8"; 
    const secretKey = "OJD0TK535GPVFA1EVFP3AEK7WWACABQW";
    const vnpUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    const returnUrl = "flowerboutique://vnpay_return"; 

    let date = new Date();
    let createDate = moment(date).format('YYYYMMDDHHmmss');
    
    let amount = parseInt(req.body.amount || req.query.amount || req.fields?.amount);
    let orderId = req.body.orderId || req.query.orderId || req.fields?.orderId;

    if (!amount || isNaN(amount) || !orderId) {
        return res.status(400).send("Lỗi: Dữ liệu không hợp lệ");
    }

    let vnp_Params = {};
    vnp_Params['vnp_Version'] = '2.1.0';
    vnp_Params['vnp_Command'] = 'pay';
    vnp_Params['vnp_TmnCode'] = tmnCode;
    vnp_Params['vnp_Locale'] = 'vn';
    vnp_Params['vnp_CurrCode'] = 'VND';
    vnp_Params['vnp_TxnRef'] = orderId;
    
    // MẸO SỐNG CÒN: Không dùng khoảng trắng để tránh lỗi encode 100%
    vnp_Params['vnp_OrderInfo'] = 'Thanh_toan_don_hang_' + orderId; 
    
    vnp_Params['vnp_OrderType'] = 'other';
    vnp_Params['vnp_Amount'] = amount * 100; 
    vnp_Params['vnp_ReturnUrl'] = returnUrl;
    vnp_Params['vnp_IpAddr'] = '127.0.0.1';
    vnp_Params['vnp_CreateDate'] = createDate;

    // 2. CHẠY HÀM SẮP XẾP VÀ MÃ HÓA
    vnp_Params = sortObject(vnp_Params);

    // 3. TẠO CHUỖI BĂM (Lưu ý: encode = false vì đã mã hóa ở hàm sortObject bên trên)
    const signData = querystring.stringify(vnp_Params, { encode: false });
    const hmac = crypto.createHmac("sha512", secretKey);
    const signed = hmac.update(Buffer.from(signData, 'utf-8')).digest("hex");
    
    vnp_Params['vnp_SecureHash'] = signed;
    
    // 4. TẠO URL CUỐI CÙNG (Lưu ý: encode = false để không bị mã hóa đè thêm 1 lần nữa)
    const finalUrl = vnpUrl + '?' + querystring.stringify(vnp_Params, { encode: false });

    console.log("URL tạo ra thành công:", finalUrl);
    res.status(200).send(finalUrl); 
});
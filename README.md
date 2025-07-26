🔗 Kolye
📌 Proje Tanımı
Kolye, Nordic Semiconductor'ın güçlü NRF52832 mikrodenetleyicisi ile geliştirilmiş, Bluetooth Low Energy (BLE) tabanlı çift yönlü kablosuz iletişim sistemidir.
Bu proje, gömülü cihazlardan (NRF52832) mobil cihazlara, mobil cihazlardan buluta ve buluttan diğer mobil cihazlara/NRF52832 cihazlarına güvenilir veri aktarımı sağlamayı hedefler.

⚙️ İşleyiş Senaryosu
📲 NRF52832 → Telefon: Cihaza bağlı bir butona basıldığında, bu olay BLE aracılığıyla anlık olarak Android telefona iletilir.

☁️ Telefon → Bulut: Telefon, aldığı bu veriyi güvenli biçimde bulut servisine gönderir.

🔔 Bulut → Diğer Telefon: Bulut, veriyi başka bir kullanıcıya ait telefona iletir, böylece gerçek zamanlı bildirim ve veri paylaşımı sağlanır.

🔄 Telefon → Diğer NRF52832: Telefon, bulut üzerinden ya da doğrudan BLE ile başka bir NRF52832 cihazına komut gönderebilir, çift yönlü etkileşim mümkün olur.

🛠️ Kullanılan Teknolojiler
🧠 NRF52832: ARM Cortex-M4 tabanlı yüksek performanslı BLE destekli mikrodenetleyici.

📡 Bluetooth Low Energy (BLE): Düşük enerji tüketimli kablosuz haberleşme protokolü.

🤖 Android: Java/Kotlin ile geliştirilen BLE destekli mobil uygulama.

☁️ Bulut Servisi: Firebase, AWS veya tercih edilen gerçek zamanlı veri tabanlarıyla entegrasyon (kullanıcıya göre değişir).

🔄 Veri Akışı: Cihazlar arası, bulut aracılığıyla gerçek zamanlı ve güvenilir iletişim.

🚀 Projenin Önemi ve Avantajları
⏱️ Gerçek Zamanlı Haberleşme: Buton basımı gibi kritik olaylar anlık olarak iletilir.

↔️ Çift Yönlü İletişim: Cihazlar ve telefonlar arasında iki yönlü veri akışı.

🌐 Bulut Entegrasyonu: Uzaktan erişim ve bildirim imkanı.

🔋 Düşük Enerji Tüketimi: NRF52832’nin BLE özellikleri sayesinde uzun pil ömrü.

🧩 Modüler ve Genişletilebilir Yapı: Farklı uygulama senaryolarına kolayca uyarlanabilir.

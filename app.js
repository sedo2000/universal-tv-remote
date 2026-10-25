const toast = document.getElementById("toast");
let toastTimer;
function notify(message) {
  toast.textContent = message;
  toast.classList.add("show");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove("show"), 4200);
}
document.getElementById("scanBtn").addEventListener("click", () => {
  notify("هذه نسخة الواجهة الأولى. البحث الحقيقي عن Android TV يحتاج إلى تطبيق Android وصلاحيات الشبكة المحلية؛ سنضيفه في المرحلة التالية.");
});
document.querySelectorAll("[data-command]").forEach((button) => {
  button.addEventListener("click", () => {
    const names = {
      power:"التشغيل والإطفاء",source:"تغيير المصدر",mute:"كتم الصوت",up:"أعلى",down:"أسفل",
      left:"يسار",right:"يمين",select:"موافق",back:"رجوع",home:"الرئيسية",
      "volume-up":"رفع الصوت","volume-down":"خفض الصوت","channel-up":"القناة التالية",
      "channel-down":"القناة السابقة",keyboard:"إدخال النص"
    };
    notify("زر «" + (names[button.dataset.command] || "التحكم") + "» جاهز في الواجهة فقط. سنفعّل إرسال الأوامر بعد بناء اتصال Android TV.");
  });
});
document.getElementById("helpBtn").addEventListener("click", () => {
  notify("اتصل الهاتف والتلفاز بشبكة Wi‑Fi نفسها. في المرحلة التالية سنضيف اكتشاف الأجهزة والاقتران الآمن. بعض التلفازات تتطلب الموافقة على شاشة التلفاز.");
});
document.getElementById("devicesLink").addEventListener("click", (event) => {
  event.preventDefault();
  document.querySelector(".devices-section").scrollIntoView({behavior:"smooth"});
});
if ("serviceWorker" in navigator && location.protocol.startsWith("http")) {
  window.addEventListener("load", () => navigator.serviceWorker.register("./sw.js").catch(() => {}));
}
let installPrompt;
const installBtn = document.getElementById("installBtn");
window.addEventListener("beforeinstallprompt", (event) => {
  event.preventDefault();
  installPrompt = event;
  installBtn.classList.remove("hidden");
});
installBtn.addEventListener("click", async () => {
  if (!installPrompt) {
    notify("من قائمة المتصفح ⋮ اختر «إضافة إلى الشاشة الرئيسية» أو «تثبيت التطبيق».");
    return;
  }
  installPrompt.prompt();
  await installPrompt.userChoice;
  installPrompt = null;
  installBtn.classList.add("hidden");
});
window.addEventListener("appinstalled", () => {
  installBtn.classList.add("hidden");
  notify("تم تثبيت الواجهة على جهازك.");
});
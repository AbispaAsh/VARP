import tkinter as tk
import qrcode
from PIL import ImageTk, Image

class QRCodeGenerator:
    def __init__(self, filenameQR, filenameCnct):
        self.root = tk.Tk()
        self.root.geometry("300x400")
        self.root['background']='#8324B2'
        self.root.configure(pady=17)
        p = ImageTk.PhotoImage(file = 'icon2.png')
        self.root.iconphoto(False, p)
        self.root.call('wm', 'iconphoto', self.root._w, p)
        self.root.title("VARP")
        self.qr_code_widget = tk.Label(self.root,borderwidth=3, relief="ridge")
        self.qr_code_widget.pack()
        self.currentQRData = ''
        self.update_qr_code(filenameQR)
        self.filenameCnct = filenameCnct
        self.currentCnctData = 'empty'
        self.text_label = tk.Label(self.root, text = "Connected Device:", font = ("Montserrat", 14), fg="#FFFFFF")
        self.text_label['background']='#8324B2'
        self.text_label.pack(pady=(20,5))
        self.text_widget = tk.Label(self.root, font = ("Montserrat",12,"bold"), fg="#FFFFFF")
        self.text_widget['background']='#8324B2'
        self.text_widget.pack()
        self.update_file()

    def update_qr_code(self, filenameQR):
        with open(filenameQR, 'r') as f:
            data = f.read()
        if(self.currentQRData != data):
            self.currentQRData == data
            qr = qrcode.QRCode(version=1, box_size=10, border=4)
            qr.add_data(data)
            qr.make(fit=True)
            img = qr.make_image(fill_color="black", back_color="white")
            img = img.resize((260,260))
            photo_image = ImageTk.PhotoImage(img)
            self.qr_code_widget.configure(image=photo_image)
            self.qr_code_widget.image = photo_image
        self.root.after(5000, self.update_qr_code, filenameQR)

    def update_file(self):
        with open(self.filenameCnct, 'r') as f:
            data = f.read()
        if(self.currentCnctData != data):
            if(len(data) == 0):
                data = "None"
            self.text_widget.configure(text = data)
            self.currentCnctData = data
        self.root.after(1000, self.update_file)

if __name__ == '__main__':
    generator = QRCodeGenerator('connectorportal.txt','connecteddevices.txt')
    generator.root.mainloop()

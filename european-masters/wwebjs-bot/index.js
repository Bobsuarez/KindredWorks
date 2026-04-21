const { Client, LocalAuth } = require('whatsapp-web.js');
const qrcode = require('qrcode-terminal');
const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const port = process.env.PORT || 3000;

const DATA_PATH = './.wwebjs_auth';

// Función para limpiar el bloqueo de Chromium (SingletonLock) de forma recursiva
function cleanChromiumLock(dir) {
    if (!fs.existsSync(dir)) return;

    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        if (fs.lstatSync(fullPath).isDirectory()) {
            cleanChromiumLock(fullPath);
        } else if (file === 'SingletonLock') {
            try {
                console.log(`Eliminando bloqueo de Chromium detectado en: ${fullPath}`);
                fs.unlinkSync(fullPath);
                console.log('Bloqueo eliminado correctamente.');
            } catch (err) {
                console.warn(`No se pudo eliminar el bloqueo en ${fullPath}:`, err.message);
            }
        }
    }
}

// Middleware para parsear JSON
app.use(express.json());

// Configuración del cliente de WhatsApp
const client = new Client({
    authStrategy: new LocalAuth({
        dataPath: DATA_PATH
    }),
    puppeteer: {
        headless: true,
        executablePath: process.env.PUPPETEER_EXECUTABLE_PATH || undefined,
        args: [
            '--no-sandbox',
            '--disable-setuid-sandbox',
            '--disable-dev-shm-usage',
            '--disable-accelerated-2d-canvas',
            '--no-first-run',
            '--no-zygote',
            '--single-process',
            '--disable-gpu'
        ],
    }
});

// Eventos de WhatsApp
client.on('qr', (qr) => {
    console.log('QR RECEIVED, SCAN IT WITH YOUR WHATSAPP APP:');
    qrcode.generate(qr, { small: true });
});

client.on('ready', () => {
    console.log('WhatsApp client is ready!');
});

client.on('authenticated', () => {
    console.log('Authenticated successfully!');
});

client.on('auth_failure', msg => {
    console.error('Authentication failure', msg);
});

// Endpoint para enviar mensajes
app.post('/send-message', async (req, res) => {
    const { numero, mensaje } = req.body;

    if (!numero || !mensaje) {
        return res.status(400).json({
            status: 'error',
            message: 'Faltan parámetros: numero y mensaje son requeridos.'
        });
    }

    try {
        // Formatear el número (agregar @c.us si no lo tiene)
        const chatId = numero.includes('@c.us') ? numero : `${numero}@c.us`;
        
        // Verificar si el cliente está listo
        const state = await client.getState();
        if (state !== 'CONNECTED') {
            return res.status(503).json({
                status: 'error',
                message: 'El cliente de WhatsApp no está conectado.'
            });
        }

        await client.sendMessage(chatId, mensaje);
        
        res.status(200).json({
            status: 'success',
            message: `Mensaje enviado a ${numero}`
        });
    } catch (error) {
        console.error('Error al enviar mensaje:', error);
        res.status(500).json({
            status: 'error',
            message: 'Error al enviar el mensaje.',
            details: error.message
        });
    }
});

// Endpoint de salud
app.get('/health', (req, res) => {
    res.status(200).json({ status: 'ok', service: 'wwebjs-bot' });
});

// Inicializar Express y WhatsApp
app.listen(port, () => {
    console.log(`API escuchando en http://localhost:${port}`);
    cleanChromiumLock(DATA_PATH);
    client.initialize();
});

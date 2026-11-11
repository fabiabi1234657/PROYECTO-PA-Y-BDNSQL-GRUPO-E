import React, { act } from 'react';
import { createRoot } from 'react-dom/client';
import axios from 'axios';
import MachineLearningView from './MachineLearningView';

jest.mock('axios');
global.IS_REACT_ACT_ENVIRONMENT = true;

let container;
let root;
const measured = {
  r2_test: 0.71, mae: 4.2, rmse: 5.1, total_muestras: 30,
  origen_datos: 'Simulación Hechos OLAP (Respaldo Druid)'
};

beforeEach(() => {
  jest.resetAllMocks();
  container = document.createElement('div');
  document.body.appendChild(container);
  root = createRoot(container);
  axios.get.mockImplementation((url) => url.endsWith('/products')
    ? Promise.resolve({ data: [{ id: 'p1', nombre: 'Arroz', precioVenta: 4600, costo: 3800 }] })
    : Promise.reject(new Error('Offline')));
});

afterEach(async () => {
  await act(async () => root.unmount());
  container.remove();
});

async function render() {
  await act(async () => root.render(<MachineLearningView />));
}

async function click(text) {
  const button = Array.from(container.querySelectorAll('button')).find((b) => b.textContent.includes(text));
  await act(async () => button.click());
}

test('disconnected service does not display invented metrics or predictions', async () => {
  await render();
  expect(container.textContent).toContain('Servicio no conectado');
  expect(container.textContent).toContain('Métricas no calculadas');
  expect(container.textContent).not.toContain('0.9289');
  axios.post.mockRejectedValue(new Error('Offline'));
  await click('Calcular Predicción');
  expect(container.textContent).toContain('Sin predicción calculada');
  expect(container.querySelector('.pred-large-num')).toBeNull();
});

test('training displays measured metrics and identifies simulated source', async () => {
  await render();
  axios.post.mockResolvedValue({ data: { metricas: measured, origen_datos: measured.origen_datos } });
  await click('Entrenar y Medir Ahora');
  expect(container.textContent).toContain('0.71');
  expect(container.textContent).toContain('Fuente: Simulación Hechos OLAP');
  expect(container.textContent).not.toContain('0.9289');
  axios.post.mockRejectedValue(new Error('Offline'));
  await click('Entrenar y Medir Ahora');
  expect(container.textContent).toContain('Métricas no calculadas');
  expect(container.querySelector('.ml-metrics-grid')).toBeNull();
});

test('reachable but untrained service leaves metrics pending', async () => {
  axios.get.mockResolvedValue({ data: { metricas: {} } });
  await render();
  expect(container.textContent).toContain('Métricas no calculadas');
  expect(container.querySelector('.ml-metrics-grid')).toBeNull();
});

/*
Purpose:
Calls the backend assistant proxy so the Groq API key never reaches the browser.
*/
import apiClient from './apiClient.js'

export const askAssistant = async ({ question, products }) => {
  const response = await apiClient.post('/assistant/chat', {
    question,
    products,
  })

  return response.data
}

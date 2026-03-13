import { useState } from 'react'

function App() {
    const [count, setCount] = useState(0)

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
            <h1 className="text-4xl font-bold text-blue-600 mb-6">
                Test Tailwind CSS v4
            </h1>

            <div className="bg-white p-8 rounded-xl shadow-md text-center">
                <p className="text-gray-700 mb-4">Click thử nút bên dưới xem sao nhé Leader!</p>
                <button
                    className="px-6 py-2 bg-blue-500 text-white font-semibold rounded-lg hover:bg-blue-600 transition duration-300"
                    onClick={() => setCount((count) => count + 1)}
                >
                    Count is {count}
                </button>
            </div>
        </div>
    )
}

export default App
// Основной JavaScript файл
document.addEventListener('DOMContentLoaded', function() {
    console.log('Hexlet Spring Blog loaded successfully!');

    // Добавляем интерактивность для заголовков
    const headings = document.querySelectorAll('h1, h2');

    headings.forEach(heading => {
        heading.addEventListener('mouseover', function() {
            this.style.color = '#e74c3c';
            this.style.transition = 'color 0.3s ease';
        });

        heading.addEventListener('mouseout', function() {
            this.style.color = '';
        });
    });

    // Добавляем анимацию для изображения
    const logo = document.querySelector('img');
    if (logo) {
        logo.addEventListener('click', function() {
            this.style.transform = 'scale(1.1)';
            setTimeout(() => {
                this.style.transform = 'scale(1)';
            }, 300);
        });
    }

    // Показываем приветственное сообщение
    setTimeout(() => {
        alert('Welcome to Hexlet Spring Blog!');
    }, 1000);
});
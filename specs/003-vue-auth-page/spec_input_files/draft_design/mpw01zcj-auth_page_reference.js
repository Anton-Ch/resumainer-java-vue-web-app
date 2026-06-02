const container = document.querySelector('.container');
const loginlink = document.querySelector('.signinlink');
const signuplink = document.querySelector('.signuplink');

signuplink.addEventListener('click',()=>{
    container.classList.add('active');
});

loginlink.addEventListener('click',()=>{
    container.classList.remove('active');
});
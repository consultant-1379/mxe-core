package utils

const SCRIPT_OBFUSCATE_SH = `#!/bin/bash
pip install --upgrade pip
# Install Cython
pip install Cython
# Creating the obfuscated code
python setup.py build_ext --inplace
# Removing the unobfuscated code
rm *.py
rm *.c
`

const SCRIPT_OBFUSCATE_SETUP_PY = `import setuptools
from distutils.core import setup
from Cython.Build import cythonize

setup(
    ext_modules = cythonize(\""*.py\"", compiler_directives={\""language_level\"" : \""3\""})
)
`
